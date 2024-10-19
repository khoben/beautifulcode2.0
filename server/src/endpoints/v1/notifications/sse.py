import asyncio
from typing import Any, AsyncIterator, Awaitable, Callable, Dict

from sse_starlette import EventSourceResponse, ServerSentEvent
from starlette.types import Receive, Scope, Send


class EventSourceResponsePatched(EventSourceResponse):
    """
    Patched: notify iterator on disconnect
    """

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        await super().__call__(scope, receive, send)
        # If still active
        if self.active:
            self.active = False
            if hasattr(self.body_iterator, "aclose"):
                await self.body_iterator.aclose()


class SSEConnection(AsyncIterator[ServerSentEvent]):
    STOP_SIGNAL = "STOP"

    def __init__(
        self, id: str, remove_handler: Callable[[str], Awaitable[None]]
    ) -> None:
        self._queue = asyncio.Queue[ServerSentEvent]()
        self._id = id
        self._remove_handler = remove_handler

    def __aiter__(self) -> "SSEConnection":
        return self

    async def __anext__(self) -> ServerSentEvent:
        event = await self._queue.get()
        self._queue.task_done()
        if event == SSEConnection.STOP_SIGNAL:
            raise StopAsyncIteration
        return event

    async def asend(self, value: ServerSentEvent) -> None:
        await self._queue.put(value)

    async def aclose(self) -> None:
        await self._remove_handler(self._id)
        await self._queue.put(SSEConnection.STOP_SIGNAL)


class SSEHandler:
    def __init__(self) -> None:
        self.__connections: Dict[str, SSEConnection] = {}

    async def create(self, id: str) -> SSEConnection:
        if id in self.__connections:
            await self.__connections[id].aclose()

        connection = SSEConnection(id=id, remove_handler=self.remove)
        self.__connections[id] = connection
        return connection

    async def remove(self, id: str) -> None:
        self.__connections.pop(id, None)

    async def send(
        self, data: Any, filter: Callable[[str, Any], Awaitable[bool]]
    ) -> None:
        await asyncio.gather(
            *[
                connection.asend(ServerSentEvent(data=data))
                for id, connection in self.__connections.items()
                if await filter(id)
            ]
        )
