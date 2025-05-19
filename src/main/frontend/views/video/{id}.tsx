import { useParams } from 'react-router-dom';
import { useEffect, useRef, useState } from "react";
import RangeSlider from 'react-range-slider-input';

import 'react-range-slider-input/dist/style.css';

export default function video() {
    const { id } = useParams();
    const videoRef = useRef<HTMLVideoElement | null>(null);
    const videoUrl = "api/v1/download/input/" + id;
    const [videoDuration, setVideoDuration] = useState(0);

    useEffect(() => {
        const videoEl = videoRef.current;

        if (!videoEl) return;

        const handleLoadedMetadata = () => {
            setVideoDuration(videoEl.duration);
        };

        videoEl.addEventListener("loadedmetadata", handleLoadedMetadata);

        return () => {
            videoEl.removeEventListener("loadedmetadata", handleLoadedMetadata);
        };
    }, [videoUrl]);

    return (
        <div className={"flex flex-col gap-2 max-w-3xl m-auto"}>
            <video controls
                   ref={videoRef}
                   width="600"
                   className={"w-full max-w-3xl rounded-lg shadow-lg border border-gray-300 bg-black m-auto"}>
                <source src={videoUrl} type="video/mp4" />
                <source src={videoUrl} type="video/webm" />
                <source src={videoUrl} type="video/ogg" />
                Your browser does not support the video tag.
            </video>

            <RangeSlider className={"w-600px"}
                        min={0}
                        max={videoDuration}/>
        </div>
    );
}