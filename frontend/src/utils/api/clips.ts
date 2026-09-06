import type { APIResponse, Clip } from "../types.ts";
import { API_URL, apiFetch } from "./client.ts";

const getClips = async (): Promise<Clip[]> => {
    const result = await apiFetch<APIResponse>(API_URL + '/api/v1/clips');
    if (result.status === 'error') throw new Error(`Failed to fetch clips: ${result.message}`);

    return result.data;
};

const getClipById = async (id: string): Promise<Clip | null> => {
    const result = await apiFetch<APIResponse>(API_URL + `/api/v1/clips/${id}`);
    if (result.status === 'error') throw new Error(`Failed to fetch clip: ${result.message}`);

    return result.data;
};

// URL for the raw media stream. The <video> element loads this directly so the
// browser can issue HTTP Range requests (progressive loading + seeking) instead
// of downloading the whole file up front.
const getClipMediaUrl = (id: string): string => API_URL + `/api/v1/clips/${id}/media`;

const patchClip = async (id: number, data: { title?: string; description?: string }): Promise<Clip> => {
    const result = await apiFetch<APIResponse>(API_URL + `/api/v1/clips/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
    });

    if (result.status === 'error') throw new Error(`Failed to update clip: ${result.message}`);

    return result.data;
};

const deleteClip = async (id: number): Promise<void> => {
    const result = await apiFetch<APIResponse>(API_URL + `/api/v1/clips/${id}`, { method: 'DELETE' });
    if (result?.status === 'error') throw new Error(`Failed to delete clip: ${result.message}`);
};

export { getClips, getClipById, getClipMediaUrl, patchClip, deleteClip };
