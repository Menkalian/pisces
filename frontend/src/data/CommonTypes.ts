export interface AudioPlayerStateData {
    isPaused: boolean,
    isRepeating: boolean,
    isShuffle: boolean,
    queueSize: number,
    nextTracks: TrackMessageData[]
}

export interface PreloadedTrackData {
    preloadUuid: string,
    displayName: string,
    preloadUrl: string,
    hasError: boolean
}

export interface UserData {
    name: string,
    avatarUrl: string
}

export interface TrackMessageData {
    title: string,
    author: string,
    state: string,
    position: number,
    length: number,
    isStream: boolean,
    sourcetype: string,
    sourceIdentifier: string,
    sourceUri: string
}

export interface CommandHelpData {
    name: string,
    description: string,
    category: string,
    parameters: ParameterHelpData[]
}

export interface ParameterHelpData {
    name: string,
    short: string,
    description: string,
    type: string,
    defaultValue: string
}
