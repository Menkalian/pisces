import mitt from "mitt"

export const EventBus = mitt()

export const EVENT_THEME_CHANGED = "themeChanged";
export const EVENT_USER_LOGIN = "loggedIn";
export const EVENT_USER_LOGOUT = "loggedOut";
export const EVENT_REFRESH_PRELOAD = "refreshPreload";