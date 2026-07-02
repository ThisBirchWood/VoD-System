import type { APIResponse, VideoMetadata } from "../types.ts";
import { API_URL } from "./client.ts";

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

const saveSection = async (startTime: string, endTime: string, title?: string, description?: string): Promise<string> => {
    const response = await fetch(API_URL + '/api/v1/media/save', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ startTime, endTime, title, description }),
    });

    const result: APIResponse = await response.json();
    if (!response.ok || result.status === 'error') throw new Error(`Failed to save section: ${result.message}`);

    return result.data.uuid;
};

const saveSectionByMarkers = async (startMarkerId: number, endMarkerId: number, title?: string, description?: string): Promise<string> => {
    const response = await fetch(API_URL + '/api/v1/media/save/markers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ startMarkerId, endMarkerId, title, description }),
    });

    const result: APIResponse = await response.json();
    if (!response.ok || result.status === 'error') throw new Error(`Failed to save section: ${result.message}`);

    return result.data.uuid;
};

const clipSection = async (duration: number, title?: string, description?: string): Promise<string> => {
    const response = await fetch(API_URL + '/api/v1/media/clip', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ duration, title, description }),
    });

    const result: APIResponse = await response.json();
    if (!response.ok || result.status === 'error') throw new Error(`Failed to clip section: ${result.message}`);

    return result.data.uuid;
};

export { compress, saveSection, saveSectionByMarkers, clipSection };
