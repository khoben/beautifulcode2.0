from typing import List, Optional

from pydantic import BaseModel, ConfigDict


class NotificationChannel(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    channel_id: Optional[str] = None
    push_enabled: Optional[bool] = None
    sms_enabled: Optional[bool] = None
    email_enabled: Optional[bool] = None


class NotificationChannelUpdate(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    push_enabled: Optional[bool] = None
    sms_enabled: Optional[bool] = None
    email_enabled: Optional[bool] = None


class UserNotificationChannels(BaseModel):
    notification_channels: List[NotificationChannel]


class NotificationEvent(BaseModel):
    title: str
    text: str
