import type { APIResponse, StreamHistoryItem, StreamStatus } from "../types.ts";
import { API_URL, apiFetch } from "./client.ts";

const getStreamStatus = async (): Promise<boolean> => {
    try {
        const result = await apiFetch<APIResponse>(API_URL + '/api/v1/stream/current');
        return result.status === 'success' && result.data?.isStreaming === true;
    } catch {
        return false;
    }
};

const getCurrentStream = async (): Promise<StreamStatus> => {
    const result = await apiFetch<APIResponse>(API_URL + '/api/v1/stream/current');
    if (result.status === 'error') throw new Error(`Failed to fetch stream status: ${result.message}`);

    return result.data;
};

const getStreamHistory = async (userId: number): Promise<StreamHistoryItem[]> => {
    const result = await apiFetch<APIResponse>(API_URL + `/api/v1/stream/history/${userId}`);
    if (result.status === 'error') throw new Error(`Failed to fetch stream history: ${result.message}`);

    return result.data;
};

export { getStreamStatus, getCurrentStream, getStreamHistory };
