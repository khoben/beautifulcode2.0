from functools import partial

from fastapi import APIRouter, HTTPException
from sse_starlette import EventSourceResponse
from starlette import status

from database import models as db_models

from ..dependencies import DbSessionDep, UserTokenDep
from .models import (
    NotificationChannel,
    NotificationChannelUpdate,
    NotificationEvent,
    UserNotificationChannels,
)
from .sse import EventSourceResponsePatched, SSEHandler

router = APIRouter()
__sse = SSEHandler()


@router.get("/channels", response_model=UserNotificationChannels)
async def get_user_notification_channels(
    user_token: UserTokenDep,
    db: DbSessionDep,
):
    notifications = (
        db.query(
            db_models.NotificationChannel.channel_id,
            db_models.NotificationChannel.sms_enabled,
            db_models.NotificationChannel.email_enabled,
            db_models.NotificationChannel.push_enabled,
        )
        .join(db_models.UserNotifications)
        .join(db_models.User)
        .filter_by(token=user_token)
        .all()
    )

    if not notifications:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Notification channels not found",
        )

    return UserNotificationChannels(
        notification_channels=[
            NotificationChannel.model_validate(notification)
            for notification in notifications
        ]
    )


@router.get(
    "/channels/{channel_id}",
    response_model=NotificationChannel,
    response_model_exclude_none=True,
)
async def get_user_notification_channel(
    channel_id: str,
    user_token: UserTokenDep,
    db: DbSessionDep,
):
    notification = (
        db.query(
            db_models.NotificationChannel.sms_enabled,
            db_models.NotificationChannel.email_enabled,
            db_models.NotificationChannel.push_enabled,
        )
        .filter_by(channel_id=channel_id)
        .join(db_models.UserNotifications)
        .join(db_models.User)
        .filter_by(token=user_token)
        .first()
    )

    if not notification:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Notification channel {} not found".format(channel_id),
        )

    return NotificationChannel.model_validate(notification)


@router.post(
    "/channels/{channel_id}",
    response_model=NotificationChannel,
    response_model_exclude_none=True,
)
async def upsert_user_notification_channel(
    user_token: UserTokenDep,
    db: DbSessionDep,
    channel_id: str,
    notification_channel: NotificationChannelUpdate,
):
    existing_notification_channel = (
        db.query(db_models.NotificationChannel)
        .filter_by(channel_id=channel_id)
        .join(db_models.UserNotifications)
        .join(db_models.User)
        .filter_by(token=user_token)
        .first()
    )

    if existing_notification_channel:
        for key, value in notification_channel.model_dump(exclude_none=True).items():
            setattr(existing_notification_channel, key, value)
        db.commit()
        return NotificationChannel.model_validate(existing_notification_channel)

    user_notifications_id = (
        db.query(db_models.UserNotifications.id)
        .join(db_models.User)
        .filter_by(token=user_token)
        .scalar()
    )

    if not user_notifications_id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User notifications not found",
        )

    new_notificaion_channel = db_models.NotificationChannel(
        channel_id=channel_id,
        user_notifications_id=user_notifications_id,
        **notification_channel.model_dump(exclude_none=True),
    )

    db.add(new_notificaion_channel)
    db.commit()

    db.refresh(new_notificaion_channel)

    return NotificationChannel.model_validate(new_notificaion_channel)


@router.post(
    "/channels/{channel_id}/event",
    status_code=status.HTTP_201_CREATED,
    response_model=NotificationEvent,
)
async def publish_notification_in_channel(
    channel_id: str,
    event: NotificationEvent,
    db: DbSessionDep,
):
    await __sse.send(data=event, filter=partial(__filter_events, db, channel_id))
    return event


async def __filter_events(db: DbSessionDep, channel_id: str, user_token: str) -> bool:
    # Filter by event channel_id and push_enabled = True
    return bool(
        db.query(db_models.NotificationChannel.id)
        .filter_by(channel_id=channel_id, push_enabled=True)
        .join(db_models.UserNotifications)
        .join(db_models.User)
        .filter_by(token=user_token)
        .first()
    )


@router.get("/events", response_class=EventSourceResponse)
async def subscribe_to_notification_events(
    user_token: UserTokenDep,
) -> EventSourceResponse:
    return EventSourceResponsePatched(await __sse.create(id=user_token))
