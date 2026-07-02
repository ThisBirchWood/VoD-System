import type { APIResponse, StreamHistoryItem, StreamStatus } from "../types.ts";
import { API_URL } from "./client.ts";

const getStreamStatus = async (): Promise<boolean> => {
    const response = await fetch(API_URL + '/api/v1/stream/current', { credentials: 'include' });
    if (!response.ok) return false;
    const result: APIResponse = await response.json();
    return result.status === 'success' && result.data?.isStreaming === true;
};

const getCurrentStream = async (): Promise<StreamStatus> => {
    const response = await fetch(API_URL + '/api/v1/stream/current', { credentials: 'include' });
    if (!response.ok) throw new Error(`Failed to fetch stream status: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Failed to fetch stream status: ${result.message}`);

    return result.data;
};

const getStreamHistory = async (userId: number): Promise<StreamHistoryItem[]> => {
    const response = await fetch(API_URL + `/api/v1/stream/history/${userId}`, { credentials: 'include' });
    if (!response.ok) throw new Error(`Failed to fetch stream history: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Failed to fetch stream history: ${result.message}`);

    return result.data;
};

export { getStreamStatus, getCurrentStream, getStreamHistory };
