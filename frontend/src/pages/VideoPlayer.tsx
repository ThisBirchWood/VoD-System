import {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import type {Clip} from "../utils/types";
import {getClipById} from "../utils/endpoints.ts";
import Box from "../components/Box.tsx"

const VideoPlayer = () => {
    const { id } = useParams();
    const [videoUrl, setVideoUrl] = useState<string>("");
    const [error, setError] = useState<string | null>(null);
    const [clip, setClip] = useState<Clip | null>(null);

    useEffect(() => {
        // Fetch the video URL from the server
        fetch(`/api/v1/download/clip/${id}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to load video");
                }
                return response.blob();
            })
            .then(blob => {
                const url = URL.createObjectURL(blob);
                setVideoUrl(url);
            })
            .catch(err => {
                console.error("Error fetching video:", err);
                setError("Failed to load video. Please try again later.");
            });

        if (!id) {
            setError("Clip ID is required.");
            return;
        }

        getClipById(id).then((fetchedClip) => {setClip(fetchedClip)})

    }, [id]);

    return (
        <div className="video-player">
            <video
                className="w-full h-full"
                controls
                autoPlay
                src={videoUrl}
                onError={(e) => {
                    setError(e.currentTarget.error?.message || "An error occurred while playing the video.");
                }}
            >
                Your browser does not support the video tag.
            </video>

            {error && <div className="text-red-500 mt-2">{error}</div>}
            {!videoUrl && !error && <div className="text-gray-500 mt-2">Loading video...</div>}

            <Box className={"p-2 m-2"}>
                <p className={"text-2xl font-bold text-gray-600"}>{clip?.title}</p>
            </Box>

        </div>
    );
};

export default VideoPlayer;