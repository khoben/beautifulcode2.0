from fastapi import FastAPI, Response

from database import models, sqlite
from endpoints import v1

models.Base.metadata.create_all(bind=sqlite.engine)

app = FastAPI(
    title="Sample Notification API", description="This is a sample notification API"
)
app.include_router(v1.router, prefix="/api")


@app.get("/health", status_code=204)
async def health():
    return Response(status_code=204)
