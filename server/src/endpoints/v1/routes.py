from fastapi import (
    APIRouter,
    Depends,
)

from .dependencies import validate_api_token
from .notifications import router as notifications_router
from .user import router as user_router

router = APIRouter(prefix="/v1")
router.include_router(user_router, prefix="/user", tags=["user"])
router.include_router(
    notifications_router,
    prefix="/notifications",
    dependencies=[Depends(validate_api_token)],
    tags=["notifications"],
)
