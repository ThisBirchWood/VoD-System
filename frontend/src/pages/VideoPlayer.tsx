import {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import type {Clip} from "../utils/types";
import {getClipById, getVideoBlob  } from "../utils/endpoints.ts";
import Box from "../components/Box.tsx"
import {dateToTimeAgo, stringToDate} from "../utils/utils.ts";

const VideoPlayer = () => {
    const { id } = useParams();
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

    // Update timeAgo live every second
    useEffect(() => {
        if (!clip || !clip.createdAt) return;

        const update = () => {
            const date = stringToDate(clip.createdAt);
            setTimeAgo(dateToTimeAgo(date));
        };

        update(); // initial update
        const interval = setInterval(update, 1000);

        return () => clearInterval(interval); // cleanup
    }, [clip]);

    return (
        <div className={"w-9/10 m-auto"}>
            <video
                className={"w-full h-full rounded-lg m-auto"}
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

            <Box className={"p-2 m-2 flex flex-col"}>
                <p className={"text-2xl font-bold text-gray-600"}>{clip?.title ? clip?.title : "(No Title)"}</p>
                <p>{timeAgo}</p>
            </Box>

        </div>
    );
};

export default VideoPlayer;