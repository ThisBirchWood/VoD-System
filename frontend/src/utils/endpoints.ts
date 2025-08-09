import type {VideoMetadata, APIResponse, User, Clip, ProgressResult } from "./types.ts";

const API_URL = import.meta.env.VITE_API_URL;

/**
 * Login function
 * @param GoogleToken - The Google token received from the frontend.
 * @return A promise that resolves to a JWT
 */
const login = async (GoogleToken: string): Promise<string> => {
    const response = await fetch(API_URL + '/api/v1/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ token: GoogleToken })
    });

    if (!response.ok) {
        throw new Error(`Login failed: ${response.status}`);
    }

    const result: APIResponse = await response.json();

    if (result.status === "error") {
        throw new Error(`Login failed: ${result.message}`);
    }

    return result.data.token;
}

/**
 * Uploads a file to the backend.
 * @param file - The file to upload.
 */
const uploadFile = async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(API_URL + '/api/v1/upload', {
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

    const response = await fetch(API_URL + `/api/v1/edit/${uuid}`, {
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
    const response = await fetch(API_URL + `/api/v1/process/${uuid}`, {credentials: "include"});

    if (!response.ok) {
        throw new Error(`Failed to process file: ${response.status}`);
    }

    const result: APIResponse = await response.json();

    if (result.status === "error") {
        throw new Error("Failed to process file: " + result.message);
    }
};

const convertFile = async (uuid: string) => {
    const response = await fetch(API_URL + `/api/v1/convert/${uuid}`);

    if (!response.ok) {
        throw new Error(`Failed to convert file: ${response.status}`);
    }

    const result: APIResponse = await response.json();

    if (result.status === "error") {
        throw new Error("Failed to convert file: " + result.message);
    }
};

/**
 * Fetches the processing progress percentage.
 * @param uuid - The UUID of the video file.
 */
const getProgress = async (uuid: string): Promise<ProgressResult> => {
    const response = await fetch(API_URL + `/api/v1/progress/${uuid}`);

    if (!response.ok) {
        throw new Error(`Failed to fetch progress: ${response.status}`);
    }

    const result = await response.json();

    if (result.status === "error") {
        throw new Error(`Failed to fetch progress: ${result.message}`);
    }

    if (!result.data) {
        throw new Error('Invalid progress data received');
    }

    return result.data;
};

/**
 * Fetches original metadata from the backend.
 * @param uuid - The UUID of the video file.
 */
const getMetadata = async (uuid: string): Promise<VideoMetadata> => {
    const response = await fetch(API_URL + `/api/v1/metadata/original/${uuid}`);

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
    const response = await fetch(API_URL + '/api/v1/auth/user', {credentials: "include"});

    if (!response.ok) {
        return null;
    }

    const result = await response.json();

    if (result.status === "error") {
        return null;
    }

    console.log(result.data);

    return result.data;
}

/**
 * Fetches all clips for the current user.
 */
const getClips = async (): Promise<Clip[]> => {
    const response = await fetch(API_URL + '/api/v1/clips', { credentials: 'include'});

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
    const response = await fetch(API_URL + `/api/v1/clips/${id}`, {credentials: "include",});

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

const isThumbnailAvailable = async (id: number): Promise<boolean> => {
    const response = await fetch(API_URL + `/api/v1/download/thumbnail/${id}`);
    if (!response.ok) {
        return false;
    }

    return true;
}

export {
    login,
    uploadFile,
    editFile,
    processFile,
    convertFile,
    getProgress,
    getMetadata,
    getUser,
    getClips,
    getClipById,
    isThumbnailAvailable
};