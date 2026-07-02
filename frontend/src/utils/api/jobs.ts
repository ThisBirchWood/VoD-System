import type { APIResponse, JobResponse } from "../types.ts";
import { API_URL } from "./client.ts";

const getJob = async (uuid: string): Promise<JobResponse> => {
    const response = await fetch(API_URL + `/api/v1/jobs/${uuid}`, { credentials: 'include' });

    if (!response.ok) throw new Error(`Failed to fetch job: ${response.status}`);

    const result: APIResponse = await response.json();
    if (result.status === 'error') throw new Error(`Failed to fetch job: ${result.message}`);

    return result.data;
};

export { getJob };
