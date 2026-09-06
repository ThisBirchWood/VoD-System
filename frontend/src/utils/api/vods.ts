import type { APIResponse, Vod } from "../types.ts";
import { API_URL, apiFetch } from "./client.ts";

const getVods = async (): Promise<Vod[]> => {
    const result = await apiFetch<APIResponse>(API_URL + '/api/v1/vods');
    if (result.status === 'error') throw new Error(`Failed to fetch vods: ${result.message}`);

    return result.data;
};

const getVodById = async (id: string): Promise<Vod | null> => {
    const result = await apiFetch<APIResponse>(API_URL + `/api/v1/vods/${id}`);
    if (result.status === 'error') throw new Error(`Failed to fetch vod: ${result.message}`);

    return result.data;
};

// URL for the raw media stream. The <video> element loads this directly so the
// browser can issue HTTP Range requests (progressive loading + seeking) instead
// of downloading the whole file up front.
const getVodMediaUrl = (id: string): string => API_URL + `/api/v1/vods/${id}/media`;

const patchVod = async (id: number, data: { title?: string; description?: string }): Promise<Vod> => {
    const result = await apiFetch<APIResponse>(API_URL + `/api/v1/vods/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
    });

    if (result.status === 'error') throw new Error(`Failed to update vod: ${result.message}`);

    return result.data;
};

const deleteVod = async (id: number): Promise<void> => {
    const result = await apiFetch<APIResponse>(API_URL + `/api/v1/vods/${id}`, { method: 'DELETE' });
    if (result?.status === 'error') throw new Error(`Failed to delete vod: ${result.message}`);
};

export { getVods, getVodById, getVodMediaUrl, patchVod, deleteVod };
