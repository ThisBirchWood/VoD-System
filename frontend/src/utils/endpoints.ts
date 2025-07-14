import type {VideoMetadata, APIResponse, User, Clip} from "./types.ts";

/**
 * Uploads a file to the backend.
 * @param file - The file to upload.
 */
const uploadFile = async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch('/api/v1/upload', {
        method: 'POST',
        body: formData,
    });

    const result: APIResponse = await response.json();

    if (result.status == "error") {
        throw new Error(`Failed to upload file: ${result.message}`);
    }

    return result.data.uuid;
};

/**
 * Submits metadata changes to the backend.
 * @param uuid - The UUID of the video file to edit.
 * @param videoMetadata - The metadata to update.
 */
const editFile = async (uuid: string, videoMetadata: VideoMetadata) => {
    const formData = new URLSearchParams();

    for (const [key, value] of Object.entries(videoMetadata)) {
        if (value !== undefined && value !== null) {
            formData.append(key, value.toString());
        }
    }

    const response = await fetch(`/api/v1/edit/${uuid}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData.toString(),
    });

    if (!response.ok) {
        throw new Error(`Failed to edit file: ${response.status}`);
    }

    const result: APIResponse = await response.json();

    if (result.status === "error") {
        throw new Error(`Failed to edit file: ${result.message}`);
    }

};

/**
 * Triggers file processing.
 * @param uuid - The UUID of the video file to process.
 */
const processFile = async (uuid: string) => {
    const response = await fetch(`/api/v1/process/${uuid}`);

    if (!response.ok) {
        throw new Error(`Failed to process file: ${response.status}`);
    }

    const result: APIResponse = await response.json();

    if (result.status === "error") {
        throw new Error("Failed to process file: " + result.message);
    }
};

/**
 * Fetches the processing progress percentage.
 * @param uuid - The UUID of the video file.
 */
const getProgress = async (uuid: string): Promise<number> => {
    const response = await fetch(`/api/v1/progress/${uuid}`);

    if (!response.ok) {
        throw new Error(`Failed to fetch progress: ${response.status}`);
    }

    const result = await response.json();

    if (result.status === "error") {
        throw new Error(`Failed to fetch progress: ${result.message}`);
    }

    if (!result.data || typeof result.data.progress !== 'number') {
        throw new Error('Invalid progress data received');
    }

    return result.data.progress;
};

/**
 * Fetches original metadata from the backend.
 * @param uuid - The UUID of the video file.
 */
const getMetadata = async (uuid: string): Promise<VideoMetadata> => {
    const response = await fetch(`/api/v1/metadata/original/${uuid}`);

    if (!response.ok) {
        throw new Error(`Failed to fetch metadata: ${response.status}`);
    }

    const result = await response.json();

    if (result.status === "error") {
        throw new Error(`Failed to fetch metadata: ${result.message}`);
    }

    return result.data;
};

/**
 * Fetches the current user information. Returns null if not authenticated.
 */
const getUser = async (): Promise<null | User > => {
    const response = await fetch('/api/v1/auth/user', {credentials: "include",});

    if (!response.ok) {
        return null;
    }

    const result = await response.json();

    if (result.status === "error") {
        return null;
    }

    return result.data;
}

/**
 * Fetches all clips for the current user.
 */
const getClips = async (): Promise<Clip[]> => {
    const response = await fetch('/api/v1/clips/', { credentials: 'include' });

    if (!response.ok) {
        const errorResult: APIResponse = await response.json();
        throw new Error(`Failed to fetch clips: ${errorResult.message}`);
    }

    try {
        const result: APIResponse = await response.json();
        return result.data;
    } catch {
        throw new Error('Failed to parse response');
    }
}

/**
 * Fetches a clip by its ID.
 * @param id
 */
const getClipById = async (id: string): Promise<Clip | null> => {
    const response = await fetch(`/api/v1/clips/${id}`, {credentials: "include",});

    if (!response.ok) {
        throw new Error(`Failed to fetch clip: ${response.status}`);
    }

    try{
        const result: APIResponse = await response.json();
        return result.data;
    } catch (error: unknown) {
        throw new Error(`Failed to parse clip response: 
        ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
};

export {
    uploadFile,
    editFile,
    processFile,
    getProgress,
    getMetadata,
    getUser,
    getClips,
    getClipById
};