type VideoMetadata = {
    title: string,
    description: string,
    startPoint: number,
    duration: number,
    fps: number,
    width: number,
    height: number,
    fileSize: number
}

type APIResponse = {
    status: string,
    data: any,
    message: string
}

type User = {
    id: number,
    name: string,
    username: string,
    email: string,
    profilePictureUrl: string,
    streamKey: string,
    createdAt: string,
}

type Clip = {
    id: number,
    userId: number,
    title: string,
    description: string,
    duration: number,
    createdAt: string,
}

type Vod = {
    id: number,
    userId: number,
    title: string,
    description: string,
    duration: number,
    createdAt: string,
}

type JobResponse = {
    uuid: string,
    progress: number,
    isComplete: boolean,
    state: 'READY' | 'PROCESSING' | 'SUCCEEDED' | 'FAILED',
    errorOutput: string | null,
    createdAt: string,
};

type StreamStatus = {
    isStreaming: boolean,
    id: number | null,
}

type StreamHistoryItem = {
    id: number,
    startDate: string | null,
    endDate: string | null,
    lastSeen: string | null,
}

type Marker = {
    id: number,
    userId: number,
    streamId: number,
    message: string,
    timestamp: string,
}

export type {
    APIResponse,
    VideoMetadata,
    User,
    Clip,
    Vod,
    JobResponse,
    StreamStatus,
    StreamHistoryItem,
    Marker
}