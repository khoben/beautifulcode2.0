from typing import List

from sqlalchemy import ForeignKey
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship


class Base(DeclarativeBase):
    __table_args__ = {"sqlite_autoincrement": True}


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    token: Mapped[str] = mapped_column(nullable=False, index=True)
    email: Mapped[str] = mapped_column(nullable=False, default="user@bank.com")
    phone: Mapped[str] = mapped_column(nullable=False, default="88005553535")
    user_notifications: Mapped["UserNotifications"] = relationship(
        back_populates="user"
    )


class UserNotifications(Base):
    __tablename__ = "user_notifications"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"))

    user: Mapped["User"] = relationship(back_populates="user_notifications")

    notifications: Mapped[List["NotificationChannel"]] = relationship(
        back_populates="user_notifications"
    )


class NotificationChannel(Base):
    __tablename__ = "notification_channels"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    channel_id: Mapped[str] = mapped_column(nullable=False, index=True)

    sms_enabled: Mapped[bool] = mapped_column(nullable=False, default=True)
    email_enabled: Mapped[bool] = mapped_column(nullable=False, default=False)
    push_enabled: Mapped[bool] = mapped_column(nullable=False, default=False)

    user_notifications_id: Mapped[int] = mapped_column(
        ForeignKey("user_notifications.id")
    )
    user_notifications: Mapped["UserNotifications"] = relationship(
        back_populates="notifications"
    )
