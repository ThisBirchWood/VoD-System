import type {VideoMetadata, APIResponse, User} from "./types.ts";

/**
 * Uploads a file to the backend.
 */
const uploadFile = async (file: File, setError: Function): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/api/v1/upload', {
            method: 'POST',
            body: formData,
        });

        const result: APIResponse = await response.json();

        if (result.status == "error") {
            setError(result.message);
        }

        return result.data.uuid;
    } catch (error: unknown) {
        throw new Error(`Failed to upload file: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
};

/**
 * Submits metadata changes to the backend.
 */
const editFile = async (
    uuid: string,
    videoMetadata: VideoMetadata,
    setError: Function ): Promise<boolean> => {
    const formData = new URLSearchParams();

    for (const [key, value] of Object.entries(videoMetadata)) {
        if (value !== undefined && value !== null) {
            formData.append(key, value.toString());
        }
    }

    try {
        const response = await fetch(`/api/v1/edit/${uuid}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData.toString(),
        });

        const result: APIResponse = await response.json();

        if (result.status === "error") {
            setError(result.message);
            return false;
        }

        return true;
    } catch (error: unknown) {
        console.error('Error editing file:', error);
        return false;
    }
};
/**
 * Triggers file processing.
 */
const processFile = async (uuid: string, setError: Function): Promise<boolean> => {
    try {
        const response = await fetch(`/api/v1/process/${uuid}`);

        const result: APIResponse = await response.json();
        if (result.status === "error") {
            setError(result.message);
            return false;
        }

        return response.ok;
    } catch (error: unknown) {
        console.error('Error processing file:', error);
        return false;
    }
};

/**
 * Fetches the processing progress percentage.
 */
const getProgress = async (uuid: string): Promise<number> => {
    try {
        const response = await fetch(`/api/v1/progress/${uuid}`);

        if (!response.ok) {
            console.error('Failed to fetch progress:', response.status);
            return 0;
        }

        const result = await response.json();
        return result.data?.progress ?? 0;
    } catch (error: unknown) {
        console.error('Error getting progress:', error);
        return 0;
    }
};

/**
 * Fetches original metadata from the backend.
 */
const getMetadata = async (uuid: string): Promise<VideoMetadata> => {
    try {
        const response = await fetch(`/api/v1/metadata/original/${uuid}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch metadata: ${response.status}`);
        }

        const result = await response.json();
        return result.data;
    } catch (error: unknown) {
        console.error('Error fetching metadata:', error);

        return {
            title: '',
            description: '',
            startPoint: 0,
            endPoint: 0,
            fps: 0,
            width: 0,
            height: 0,
            fileSize: 0,
        };
    }
};

const getUser = async (): Promise<null | User > => {
    try {
        const response = await fetch('/api/v1/auth/user', {credentials: "include",});

        const result = await response.json();
        return result.data;
    } catch (error: unknown) {
        console.error('Error fetching user:', error);
        return null;
    }
}

export {
    uploadFile,
    editFile,
    processFile,
    getProgress,
    getMetadata,
    getUser
};