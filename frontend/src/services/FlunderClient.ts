import type {AxiosInstance, AxiosResponse} from "axios"
import axios from "axios"
// @ts-ignore
import SockJS from "sockjs-client";
import Stomp, {Client} from "webstomp-client"
import type {AudioPlayerStateData, CommandHelpData, PreloadedTrackData, TrackMessageData, UserData} from "@/data/CommonTypes";

// noinspection JSUnusedGlobalSymbols
export class FlunderClient {
    public subscribeAudioInfo(guildId: string, listener: (d: TrackMessageData) => void): () => void {
        let socket = this.connectSocket()
        socket.connect({}, _ => {
            socket.subscribe(`/audio/${guildId}`, data => {
                listener(JSON.parse(data.body) as TrackMessageData)
            })
        })
        return () => {
            socket.unsubscribe(`/audio/${guildId}`)
        }
    }

    public subscribePlayerInfo(guildId: string, listener: (d: AudioPlayerStateData) => void): () => void {
        let socket = this.connectSocket()
        socket.connect({}, _ => {
            socket.subscribe(`/player/${guildId}`, data => {
                listener(JSON.parse(data.body) as AudioPlayerStateData)
            })
        })
        return () => {
            socket.unsubscribe(`/player/${guildId}`)
        }
    }

    public async getUserData(): Promise<UserData> {
        return this
            .baseRequest()
            .get<UserData>("/user/details")
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async getHelpData(): Promise<Map<string, string[]>> {
        return this
            .baseRequest()
            .get<Map<string, string[]>>("/help/commands")
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async getCommandHelpData(command: string): Promise<CommandHelpData> {
        return this
            .baseRequest()
            .get<CommandHelpData>(`/help/command/${command}`)
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async getConnectedGuild(): Promise<string> {
        return this
            .baseRequest()
            .get<string>("/audio/guild")
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async playTrack(searchterm: string): Promise<TrackMessageData> {
        return this
            .baseRequest()
            .post<TrackMessageData>("/audio/control/play", searchterm)
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async skipTrack(): Promise<boolean> {
        return this
            .baseRequest()
            .get<boolean>("/audio/control/skip")
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async skipBy(amount: number): Promise<TrackMessageData> {
        return this
            .baseRequest()
            .post<TrackMessageData>("/audio/control/skipby", amount)
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async toggleLoop(): Promise<boolean> {
        return this
            .baseRequest()
            .get<boolean>("/audio/control/toggle/loop")
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async toggleShuffle(): Promise<boolean> {
        return this
            .baseRequest()
            .get<boolean>("/audio/control/toggle/shuffle")
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async togglePause(): Promise<boolean> {
        return this
            .baseRequest()
            .get<boolean>("/audio/control/toggle/pause")
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async getGlobalPreloadedTracks(): Promise<PreloadedTrackData[]> {
        return this
            .baseRequest()
            .get<PreloadedTrackData[]>("preload/global")
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async preloadGlobalTrack(input: PreloadedTrackData): Promise<PreloadedTrackData> {
        return this
            .baseRequest()
            .post<PreloadedTrackData>("preload/global", input)
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async playGlobalPreloadedTrack(uuid: string, instant: boolean = false, interject: boolean = false): Promise<boolean> {
        return this
            .baseRequest()
            .get<boolean>(`preload/global/${uuid}`, {
                params: {
                    instant: instant,
                    interject: interject
                }
            })
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async deleteGlobalPreloadedTrack(uuid: string): Promise<boolean> {
        return this
            .baseRequest()
            .delete<boolean>(`preload/global/${uuid}`)
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async getPersonalPreloadedTracks(): Promise<PreloadedTrackData[]> {
        return this
            .baseRequest()
            .get<PreloadedTrackData[]>("preload/personal")
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async preloadPersonalTrack(input: PreloadedTrackData): Promise<PreloadedTrackData> {
        return this
            .baseRequest()
            .post<PreloadedTrackData>("preload/personal", input)
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async playPersonalPreloadedTrack(uuid: string, instant: boolean = false, interject: boolean = false): Promise<boolean> {
        return this
            .baseRequest()
            .get<boolean>(`preload/personal/${uuid}`, {
                params: {
                    instant: instant,
                    interject: interject
                }
            })
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    public async deletePersonalPreloadedTrack(uuid: string): Promise<boolean> {
        return this
            .baseRequest()
            .delete<boolean>(`preload/personal/${uuid}`)
            .then((response: AxiosResponse) => {
                if (response.status >= 400) {
                    throw new Error(response.statusText)
                }
                return response.data
            })
    }

    private connectSocket(): Client {
        let socket = new SockJS("https://flunder.menkalian.de/audioinfo/live")
        return Stomp.over(socket)
    }

    private baseRequest(): AxiosInstance {
        return axios.create(
            {
                baseURL: "https://flunder.menkalian.de",
                xsrfCookieName: "XSRF-TOKEN",
                withCredentials: true,
            })
    }
}
