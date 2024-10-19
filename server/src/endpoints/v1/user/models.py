from typing import Optional

from pydantic import BaseModel, ConfigDict


class UserToken(BaseModel):
    token: str


class UserProfile(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    email: Optional[str] = None
    phone: Optional[str] = None
