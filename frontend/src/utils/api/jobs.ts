import type { APIResponse, JobResponse } from "../types.ts";
import { API_URL, apiFetch, apiFetchRaw } from "./client.ts";

const getJob = async (uuid: string): Promise<JobResponse> => {
    const result = await apiFetch<APIResponse>(API_URL + `/api/v1/jobs/${uuid}`);
    if (result.status === 'error') throw new Error(`Failed to fetch job: ${result.message}`);

    return result.data;
};

const downloadJob = async (uuid: string): Promise<Blob> => {
    const response = await apiFetchRaw(API_URL + `/api/v1/jobs/${uuid}/download`);
    return response.blob();
};

export { getJob, downloadJob };
