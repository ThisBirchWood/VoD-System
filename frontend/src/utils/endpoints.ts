import type { VideoMetadata, APIResponse, User, Clip, Vod, JobResponse } from "./types.ts";

const API_URL = import.meta.env.VITE_API_URL;

export class AuthError extends Error {
    constructor() { super("Not authenticated"); this.name = "AuthError"; }
}

const login = async (googleToken: string): Promise<string> => {
    const response = await fetch(API_URL + '/api/v1/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token: googleToken }),
        credentials: 'include',
    });

    if (!response.ok) throw new Error(`Login failed: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Login failed: ${result.message}`);

    return result.data.token;
};

const logout = async (): Promise<void> => {
    const response = await fetch(API_URL + '/api/v1/users/logout', {
        method: 'POST',
        credentials: 'include',
    });

    if (!response.ok) throw new Error(`Logout failed: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Logout failed: ${result.message}`);
};

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

    if (!response.ok) throw new Error(`Compression failed: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Compression failed: ${result.message}`);

    return result.data.uuid;
};

const getJob = async (uuid: string): Promise<JobResponse> => {
    const response = await fetch(API_URL + `/api/v1/jobs/${uuid}`, { credentials: 'include' });

    if (!response.ok) throw new Error(`Failed to fetch job: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Failed to fetch job: ${result.message}`);

    return result.data;
};

const getUser = async (): Promise<User | null> => {
    const response = await fetch(API_URL + '/api/v1/users/me', { credentials: 'include' });
    if (!response.ok) return null;

    const result: APIResponse = await response.json();
    if (result.status === 'error') return null;

    return result.data;
};

const getClips = async (): Promise<Clip[]> => {
    const response = await fetch(API_URL + '/api/v1/clips', { credentials: 'include' });

    if (!response.ok) {
        const errorResult: APIResponse = await response.json();
        throw new Error(`Failed to fetch clips: ${errorResult.message}`);
    }

    const result: APIResponse = await response.json();
    return result.data;
};

const getClipById = async (id: string): Promise<Clip | null> => {
    const response = await fetch(API_URL + `/api/v1/clips/${id}`, { credentials: 'include' });

    if (!response.ok) {
        if (response.status === 401 || response.status === 403) throw new AuthError();
        throw new Error(`Failed to fetch clip: ${response.status}`);
    }

    const result: APIResponse = await response.json();
    return result.data;
};

const getVideoBlob = async (id: string): Promise<Blob> => {
    const response = await fetch(API_URL + `/api/v1/clips/${id}/media`, { credentials: 'include' });

    if (!response.ok) {
        if (response.status === 401 || response.status === 403) throw new AuthError();
        throw new Error(`Failed to fetch video ${id}: ${response.status}`);
    }

    return response.blob();
};

const patchClip = async (id: number, data: { title?: string; description?: string }): Promise<Clip> => {
    const response = await fetch(API_URL + `/api/v1/clips/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(data),
    });

    if (!response.ok) throw new Error(`Failed to update clip: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Failed to update clip: ${result.message}`);

    return result.data;
};

const deleteClip = async (id: number): Promise<void> => {
    const response = await fetch(API_URL + `/api/v1/clips/${id}`, {
        method: 'DELETE',
        credentials: 'include',
    });

    if (!response.ok) throw new Error(`Failed to delete clip: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Failed to delete clip: ${result.message}`);
};

const getVods = async (): Promise<Vod[]> => {
    const response = await fetch(API_URL + '/api/v1/vods', { credentials: 'include' });

    if (!response.ok) {
        const errorResult: APIResponse = await response.json();
        throw new Error(`Failed to fetch vods: ${errorResult.message}`);
    }

    const result: APIResponse = await response.json();
    return result.data;
};

const getVodById = async (id: string): Promise<Vod | null> => {
    const response = await fetch(API_URL + `/api/v1/vods/${id}`, { credentials: 'include' });

    if (!response.ok) {
        if (response.status === 401 || response.status === 403) throw new AuthError();
        throw new Error(`Failed to fetch vod: ${response.status}`);
    }

    const result: APIResponse = await response.json();
    return result.data;
};

const getVodBlob = async (id: string): Promise<Blob> => {
    const response = await fetch(API_URL + `/api/v1/vods/${id}/media`, { credentials: 'include' });

    if (!response.ok) {
        if (response.status === 401 || response.status === 403) throw new AuthError();
        throw new Error(`Failed to fetch vod ${id}: ${response.status}`);
    }

    return response.blob();
};

const patchVod = async (id: number, data: { title?: string; description?: string }): Promise<Vod> => {
    const response = await fetch(API_URL + `/api/v1/vods/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(data),
    });

    if (!response.ok) throw new Error(`Failed to update vod: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Failed to update vod: ${result.message}`);

    return result.data;
};

const deleteVod = async (id: number): Promise<void> => {
    const response = await fetch(API_URL + `/api/v1/vods/${id}`, {
        method: 'DELETE',
        credentials: 'include',
    });

    if (!response.ok) throw new Error(`Failed to delete vod: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Failed to delete vod: ${result.message}`);
};

const getStreamStatus = async (): Promise<boolean> => {
    const response = await fetch(API_URL + '/api/v1/stream/current', { credentials: 'include' });
    if (!response.ok) return false;
    const result: APIResponse = await response.json();
    return result.status === 'success' && result.data?.isStreaming === true;
};

const isThumbnailAvailable = async (thumbnailUrl: string): Promise<boolean> => {
    const response = await fetch(thumbnailUrl, { credentials: 'include' });
    return response.ok;
};

export {
    login,
    logout,
    compress,
    getJob,
    getUser,
    getClips,
    getClipById,
    getVideoBlob,
    patchClip,
    deleteClip,
    getVods,
    getVodById,
    getVodBlob,
    patchVod,
    deleteVod,
    getStreamStatus,
    isThumbnailAvailable,
};
