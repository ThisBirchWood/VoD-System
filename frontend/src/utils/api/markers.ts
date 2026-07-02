import type { APIResponse, Marker } from "../types.ts";
import { API_URL } from "./client.ts";

const getMarkers = async (): Promise<Marker[]> => {
    const response = await fetch(API_URL + '/api/v1/markers', { credentials: 'include' });
    if (!response.ok) throw new Error(`Failed to fetch markers: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Failed to fetch markers: ${result.message}`);

    return result.data;
};

const createMarker = async (message: string): Promise<Marker> => {
    const response = await fetch(API_URL + '/api/v1/markers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ message }),
    });

    const result: APIResponse = await response.json();
    if (!response.ok || result.status === 'error') throw new Error(`Failed to add marker: ${result.message}`);

    return result.data;
};

const deleteMarker = async (id: number): Promise<void> => {
    const response = await fetch(API_URL + `/api/v1/markers/${id}`, {
        method: 'DELETE',
        credentials: 'include',
    });

    if (!response.ok) throw new Error(`Failed to delete marker: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Failed to delete marker: ${result.message}`);
};

export { getMarkers, createMarker, deleteMarker };
