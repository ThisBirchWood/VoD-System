import { useParams } from 'react-router-dom';
import { useEffect, useRef, useState } from "react";
import { VideoMetadata } from "Frontend/components/Playbar";
import Playbar from "./../../components/Playbar";
import PlaybackSlider from "./../../components/PlaybackSlider";
import ClipRangeSlider from "./../../components/ClipRangeSlider";
import ClipConfig from "./../../components/ClipConfig";

export default function VideoId() {
    const { id } = useParams();
    const videoRef = useRef<HTMLVideoElement | null>(null);
    const videoUrl = `api/v1/download/input/${id}`

    const [metadata, setMetadata] = useState<VideoMetadata | null>(null);
    const [playbackValue, setPlaybackValue] = useState(0);
    const [clipRangeValue, setClipRangeValue] = useState([0, 1]);

    useEffect(() => {
        fetch(`api/v1/metadata/original/${id}`)
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch metadata");
                return res.json();
            })
            .then(setMetadata)
            .catch((err) => console.log(err.message));
    }, [id]);

    return (
        <div className={"grid grid-cols-[70%_30%]"}>
            <video
                   ref={videoRef}
                   className={"w-full rounded-lg shadow-lg border border-gray-300 bg-black m-auto"}>
                <source src={videoUrl} type="video/mp4" />
                <source src={videoUrl} type="video/webm" />
                <source src={videoUrl} type="video/ogg" />
                Your browser does not support the video tag. Bzzzz.
            </video>


            <ClipConfig />

            {metadata &&
                <div>
                    <Playbar
                        video={videoRef.current}
                        videoMetadata={metadata}
                        className={"w-full accent-primary text-text"}
                    />

                    <PlaybackSlider
                        videoRef={videoRef.current}
                        videoMetadata={metadata}
                        sliderValue={playbackValue}
                        setSliderValue={setPlaybackValue}
                        className={"w-full accent-primary"}
                    />

                    <ClipRangeSlider
                        videoRef={videoRef.current}
                        videoMetadata={metadata}
                        setSliderValue={setPlaybackValue}
                        setClipRangeValue={setClipRangeValue}
                        className={"w-full mb-10 bg-primary"}
                    />
                </div>}

            <button
                className={"bg-primary text-text p-2 rounded-lg hover:bg-primary-pressed h-10 w-3/4 m-auto"}
            >Export</button>

        </div>
    );
}