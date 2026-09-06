import type { APIResponse, Marker } from "../types.ts";
import { API_URL, apiFetch } from "./client.ts";

const getMarkers = async (): Promise<Marker[]> => {
    const result = await apiFetch<APIResponse>(API_URL + '/api/v1/markers');
    if (result.status === 'error') throw new Error(`Failed to fetch markers: ${result.message}`);

    return result.data;
};

const createMarker = async (message: string): Promise<Marker> => {
    const result = await apiFetch<APIResponse>(API_URL + '/api/v1/markers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message }),
    });

    if (result.status === 'error') throw new Error(`Failed to add marker: ${result.message}`);

    return result.data;
};

const deleteMarker = async (id: number): Promise<void> => {
    const result = await apiFetch<APIResponse>(API_URL + `/api/v1/markers/${id}`, { method: 'DELETE' });
    if (result?.status === 'error') throw new Error(`Failed to delete marker: ${result.message}`);
};

export { getMarkers, createMarker, deleteMarker };
