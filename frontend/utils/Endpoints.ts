type VideoMetadata = {
    startPoint: number,
    endPoint: number,
    fps: number,
    width: number,
    height: number,
    fileSize: number
}

const uploadFile = async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('api/v1/upload', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            console.log("File uploaded successfully");
            return await response.text();
        } else {
            console.log("File upload failed");
            return "";
        }

    } catch (error) {
        console.error('Error uploading file:', error);
        return "";
    }
};

const editFile = async (uuid: string, videoMetadata: VideoMetadata): Promise<boolean> => {
    const formData = new URLSearchParams();
    Object.entries(videoMetadata).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
            formData.append(key, value.toString());
        }
    });

    try {
        const response = await fetch(`/api/v1/edit/${uuid}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData.toString()
        });

        return response.ok;
    } catch (error){
        console.error('Error editing file:', error);
        return false;
    }
}

const processFile = async (uuid: string): Promise<boolean> => {
    const response = await fetch(`/api/v1/process/${uuid}`);
    return response.ok;
}

const getProgress = async (uuid: string): Promise<number> => {
    const response = await fetch(`/api/v1/progress/${uuid}`);

    if (response.ok) {
        return response.json();
    } else {
        return 0;
    }
}

const getMetadata = async (uuid: string): Promise<VideoMetadata> => {
    try {
        const response = await fetch(`/api/v1/metadata/${uuid}`);
        return response.json();
    } catch (error) {
        return {
            startPoint: 0,
            endPoint: 0,
            fps: 0,
            width: 0,
            height: 0,
            fileSize: 0
        }
    }
}

export {
    VideoMetadata,
    uploadFile,
    editFile,
    processFile,
    getProgress,
    getMetadata
}