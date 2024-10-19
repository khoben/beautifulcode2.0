import uuid

from fastapi import APIRouter, HTTPException
from starlette import status

from database import models as db_models

from ..dependencies import DbSessionDep, MaybeUserTokenDep, UserTokenDep
from .models import UserProfile, UserToken

router = APIRouter()


@router.post("/auth", response_model=UserToken)
async def register_user(
    db: DbSessionDep,
    user_token: MaybeUserTokenDep,
):
    is_token_exists = user_token and bool(
        db.query(db_models.User.id).filter(db_models.User.token == user_token).first()
    )

    if not is_token_exists:
        new_token = str(uuid.uuid4())

        new_user = db_models.User(
            token=new_token,
            user_notifications=db_models.UserNotifications(
                notifications=[
                    db_models.NotificationChannel(channel_id="transactions"),
                    db_models.NotificationChannel(channel_id="deposits"),
                    db_models.NotificationChannel(channel_id="withdrawals"),
                    db_models.NotificationChannel(channel_id="confirmations"),
                    db_models.NotificationChannel(channel_id="promotions"),
                    db_models.NotificationChannel(channel_id="chat"),
                    db_models.NotificationChannel(channel_id="support"),
                ]
            ),
        )

        db.add(new_user)
        db.commit()

        token = new_token
    else:
        token = user_token

    return UserToken(token=token)


@router.get("/profile", response_model=UserProfile)
async def get_user_profile(
    db: DbSessionDep,
    user_token: UserTokenDep,
):
    user = db.query(db_models.User).filter(db_models.User.token == user_token).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found",
        )

    return UserProfile.model_validate(user)


@router.post("/profile", response_model=UserProfile)
async def update_user_profile(
    db: DbSessionDep, user_token: UserTokenDep, user_profile: UserProfile
):
    existing_user = (
        db.query(db_models.User).filter(db_models.User.token == user_token).first()
    )
    if not existing_user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found",
        )

    for key, value in user_profile.model_dump(exclude_none=True).items():
        setattr(existing_user, key, value)

    db.commit()

    return UserProfile.model_validate(existing_user)
