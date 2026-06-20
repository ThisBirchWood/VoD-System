import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import type {Clip} from "../utils/types";
import {getClipById, getVideoBlob} from "../utils/endpoints.ts";
import Box from "../components/Box.tsx"
import {dateToTimeAgo, stringToDate} from "../utils/utils.ts";

const VideoPlayer = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [videoUrl, setVideoUrl] = useState<string | undefined>(undefined);
    const [error, setError] = useState<string | null>(null);
    const [clip, setClip] = useState<Clip | null>(null);
    const [timeAgo, setTimeAgo] = useState<String>("");

    useEffect(() => {
        if (!id) {
            setError("Clip ID is required.");
            return;
        }

        getVideoBlob(id)
            .then((blob) => {
                const url = URL.createObjectURL(blob);
                setVideoUrl(url);
            })

        getClipById(id)
            .then((fetchedClip) => {setClip(fetchedClip)})
            .catch((err) => {
                console.error("Error fetching clip:", err);
                setError("Failed to load clip details. Please try again later.");
            });

    }, [id]);

    useEffect(() => {
        if (!clip || !clip.createdAt) return;

        const update = () => {
            const date = stringToDate(clip.createdAt);
            setTimeAgo(dateToTimeAgo(date));
        };

        update();
        const interval = setInterval(update, 1000);

        return () => clearInterval(interval);
    }, [clip]);

    return (
        <div className="p-6 max-w-5xl mx-auto">
            <button
                onClick={() => navigate(-1)}
                className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-900 mb-4 transition-colors duration-150"
            >
                <ArrowLeft size={16} />
                Back
            </button>

            <video
                className="w-full rounded-xl shadow-md bg-black"
                controls
                autoPlay
                src={videoUrl}
                onError={(e) => {
                    setError(e.currentTarget.error?.message || "An error occurred while playing the video.");
                }}
            >
                Your browser does not support the video tag.
            </video>

            {error && <div className="text-red-500 text-sm mt-3">{error}</div>}
            {!videoUrl && !error && <div className="text-gray-400 text-sm mt-3">Loading video...</div>}

            <Box className="p-4 mt-4">
                <p className="text-xl font-semibold text-gray-900">{clip?.title || "(No Title)"}</p>
                <p className="text-sm text-gray-500 mt-1">{timeAgo}</p>
            </Box>
        </div>
    );
};

export default VideoPlayer;
