import {useEffect, useState} from "react";
import {useParams} from "react-router-dom";

const VideoPlayer = () => {
    const { id } = useParams();
    const [videoUrl, setVideoUrl] = useState<string>("");
    const [error, setError] = useState<string | null>(null);

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
        </div>
    );
};

export default VideoPlayer;