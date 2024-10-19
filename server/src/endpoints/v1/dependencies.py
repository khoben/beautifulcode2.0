from typing import Annotated, Optional

from fastapi import Depends, HTTPException, Security
from fastapi.security import APIKeyHeader
from sqlalchemy.orm import Session

from database import get_session
from database import models as db_models

__all__ = ["UserTokenDep", "DbSessionDep"]

DbSessionDep = Annotated[Session, Depends(get_session)]

API_TOKEN_HEADER = APIKeyHeader(name="X-API-TOKEN", auto_error=False)


async def validate_api_token(
    token: Annotated[Optional[str], Security(API_TOKEN_HEADER)] = None,
):
    if not token:
        raise HTTPException(status_code=401, detail="Missing or invalid API token")
    return None


async def provide_user_token(
    db: DbSessionDep,
    user_token: Annotated[Optional[str], Depends(API_TOKEN_HEADER)] = None,
):
    if not user_token:
        raise HTTPException(status_code=401, detail="Missing or invalid API token")

    if bool(
        db.query(db_models.User.id).filter(db_models.User.token == user_token).first()
    ):
        return user_token

    raise HTTPException(status_code=401, detail="Missing or invalid API token")


UserTokenDep = Annotated[str, Depends(provide_user_token)]
MaybeUserTokenDep = Annotated[Optional[str], Security(API_TOKEN_HEADER)]
