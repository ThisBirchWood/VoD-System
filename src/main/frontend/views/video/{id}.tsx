import { useParams } from 'react-router-dom';
import { useEffect, useRef, useState } from "react";
import { VideoMetadata } from "Frontend/components/Playbar";
import Playbar from "./../../components/Playbar"
import PlaybackSlider from "./../../components/PlaybackSlider"
import ClipRangeSlider from "./../../components/ClipRangeSlider"

export default function VideoId() {
    const { id } = useParams();
    const videoRef = useRef<HTMLVideoElement | null>(null);
    const videoUrl = `api/v1/download/input/${id}`

    const [metadata, setMetadata] = useState<VideoMetadata | null>(null);
    const [sliderValue, setSliderValue] = useState(0);

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
        <div className={"flex flex-col gap-2 max-w-3xl m-auto align-middle"}>
            <video
                   ref={videoRef}
                   width={"600"}
                   className={"w-full max-w-3xl rounded-lg shadow-lg border border-gray-300 bg-black m-auto"}>
                <source src={videoUrl} type="video/mp4" />
                <source src={videoUrl} type="video/webm" />
                <source src={videoUrl} type="video/ogg" />
                Your browser does not support the video tag. Bzzzz.
            </video>


            {metadata &&
                <div>
                    <Playbar video={videoRef.current} videoMetadata={metadata}/>
                    <PlaybackSlider
                        videoRef={videoRef.current}
                        videoMetadata={metadata}
                        sliderValue={sliderValue}
                        setSliderValue={setSliderValue}
                        className={"w-full"}
                    />

                    <ClipRangeSlider
                        videoRef={videoRef.current}
                        videoMetadata={metadata}
                        setSliderValue={setSliderValue}
                        className={"w-full"}
                    />
                </div>
            }
        </div>
    );
}