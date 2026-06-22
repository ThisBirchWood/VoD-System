import type {VideoMetadata, APIResponse, User, Clip, JobResponse } from "./types.ts";

const API_URL = import.meta.env.VITE_API_URL;

export class AuthError extends Error {
    constructor() { super("Not authenticated"); this.name = "AuthError"; }
}

/**
 * Login function
 * @param GoogleToken - The Google token received from the frontend.
 * @return A promise that resolves to a JWT
 */
const login = async (GoogleToken: string): Promise<string> => {
    const response = await fetch(API_URL + '/api/v1/users/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ token: GoogleToken }),
        credentials: 'include'
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

const logout = async () => {
    const response = await fetch(API_URL + '/api/v1/users/logout', {
        method: 'POST',
        credentials: 'include'
    });

    if (!response.ok) {
        throw new Error(`Logout failed: ${response.status}`);
    }

    const result: APIResponse = await response.json();

    if (result.status === "error") {
        throw new Error(`Logout failed: ${result.message}`);
    }
}

/**
 * Uploads a file and starts compression in one step.
 * @param file - The file to compress.
 * @param options - The clip/compression options.
 * @returns The job UUID to poll for progress.
 */
const compress = async (file: File, options: VideoMetadata): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', options.title);
    if (options.description) formData.append('description', options.description);
    formData.append('startPoint', options.startPoint.toString());
    formData.append('duration', options.duration.toString());
    if (options.fps) formData.append('fps', options.fps.toString());
    if (options.width) formData.append('width', options.width.toString());
    if (options.height) formData.append('height', options.height.toString());
    if (options.fileSize) formData.append('fileSize', options.fileSize.toString());

    const response = await fetch(API_URL + '/api/v1/media/compress', {
        method: 'POST',
        body: formData,
        credentials: 'include',
    });

    if (!response.ok) {
        throw new Error(`Compression failed: ${response.status}`);
    }

    const result: APIResponse = await response.json();

    if (result.status === 'error') {
        throw new Error(`Compression failed: ${result.message}`);
    }

    return result.data.uuid;
};

/**
 * Fetches the status of a background job.
 * @param uuid - The job UUID.
 */
const getJob = async (uuid: string): Promise<JobResponse> => {
    const response = await fetch(API_URL + `/api/v1/jobs/${uuid}`, { credentials: 'include' });

    if (!response.ok) {
        throw new Error(`Failed to fetch job: ${response.status}`);
    }

    const result: APIResponse = await response.json();

    if (result.status === 'error') {
        throw new Error(`Failed to fetch job: ${result.message}`);
    }

    return result.data;
};

/**
 * Fetches the current user information. Returns null if not authenticated.
 */
const getUser = async (): Promise<null | User > => {
    const response = await fetch(API_URL + '/api/v1/users/me', {credentials: "include"});

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
        if (response.status === 401 || response.status === 403) throw new AuthError();
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

const getVideoBlob = async(id: string): Promise<Blob> => {
    const response = await fetch(API_URL + `/api/v1/clips/${id}/media`, {credentials: "include",});

    if (!response.ok) {
        if (response.status === 401 || response.status === 403) throw new AuthError();
        throw new Error(`Failed to fetch video: ${id}: ${response.status}`)
    }

    try {
        return response.blob();
    } catch {
        throw new Error(`Failed to convert Clip Return Object to blob`);
    }
}

const patchClip = async (id: number, data: { title?: string; description?: string }): Promise<Clip> => {
    const response = await fetch(API_URL + `/api/v1/clips/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(data),
    });

    if (!response.ok) {
        throw new Error(`Failed to update clip: ${response.status}`);
    }

    const result: APIResponse = await response.json();
    if (result.status === 'error') {
        throw new Error(`Failed to update clip: ${result.message}`);
    }

    return result.data;
};

const deleteClip = async (id: number): Promise<void> => {
    const response = await fetch(API_URL + `/api/v1/clips/${id}`, {
        method: 'DELETE',
        credentials: 'include',
    });

    if (!response.ok) {
        throw new Error(`Failed to delete clip: ${response.status}`);
    }

    const result: APIResponse = await response.json();
    if (result.status === 'error') {
        throw new Error(`Failed to delete clip: ${result.message}`);
    }
};

const getStreamStatus = async (): Promise<boolean> => {
    const response = await fetch(API_URL + '/api/v1/stream/current', { credentials: 'include' });
    if (!response.ok) return false;
    const result: APIResponse = await response.json();
    return result.status === 'success' && result.data?.isStreaming === true;
};

const isThumbnailAvailable = async (id: number): Promise<boolean> => {
    const response = await fetch(API_URL + `/api/v1/clips/${id}/thumbnail`, {credentials: "include"});
    return response.ok;
}

export {
    login,
    logout,
    getStreamStatus,
    compress,
    getJob,
    getUser,
    getClips,
    getClipById,
    getVideoBlob,
    patchClip,
    deleteClip,
    isThumbnailAvailable
};