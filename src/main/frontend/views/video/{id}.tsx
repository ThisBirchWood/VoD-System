import { useParams } from 'react-router-dom';
import { useEffect, useRef, useState } from "react";
import RangeSlider from 'react-range-slider-input';
import 'react-range-slider-input/dist/style.css';


export type VideoMetadata = {
    startPoint: number,
    endPoint: number,
    fps: number,
    width: number,
    height: number,
    fileSize: number
}

const fetchMetadata = async (id: string): Promise<VideoMetadata> => {
    const res = await fetch(`/api/v1/metadata/original/${id}`);
    if (!res.ok) throw new Error("Failed to fetch");
    return res.json();
};

export default function video() {
    const { id } = useParams();
    const videoRef = useRef<HTMLVideoElement | null>(null);
    const videoUrl = "api/v1/download/input/" + id;

    const [metadata, setMetadata] = useState<VideoMetadata | null>(null);
    const [sliderValue, setSliderValue] = useState(0);

    let previousRangeSliderInput = useRef<[number, number]>([0, 0]);
    const handleRangeSliderInput = (val: [number, number]) => {
        if (!videoRef.current) {
            return;
        }

        if (previousRangeSliderInput.current[0] != val[0]) {
            videoRef.current.currentTime = val[0];
            setSliderValue(val[0]);
        } else if (previousRangeSliderInput.current[1] != val[1]) {
            videoRef.current.currentTime = val[1];
            setSliderValue(val[1]);
        }

        previousRangeSliderInput.current = val;
    };

    const updateVideoTag = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!videoRef.current) {
            return;
        }

        setSliderValue(parseFloat(e.target.value));
        videoRef.current.currentTime = parseFloat(e.target.value);
    };

    useEffect(() => {
        fetch(`api/v1/metadata/original/${id}`)
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch metadata");
                return res.json();
            })
            .then(setMetadata)
            .catch((err) => console.log(err.message));
    }, []);

    return (
        <div className={"flex flex-col gap-2 max-w-3xl m-auto"}>
            <video
                   ref={videoRef}
                   preload="metadata"
                   width="600"
                   className={"w-full max-w-3xl rounded-lg shadow-lg border border-gray-300 bg-black m-auto"}>
                <source src={videoUrl} type="video/mp4" />
                <source src={videoUrl} type="video/webm" />
                <source src={videoUrl} type="video/ogg" />
                Your browser does not support the video tag. Bzzzz.
            </video>


            {metadata &&
                <div>
                    <input
                        className={"w-full"}
                        type="range"
                        min={0}
                        max={metadata.endPoint}
                        value={sliderValue}
                        onChange={updateVideoTag}
                        step={0.1}
                    />

                    <RangeSlider
                                 min={0}
                                 max={metadata.endPoint}
                                 step={0.1}
                                 onInput={handleRangeSliderInput}/>
                </div>
            }
        </div>
    );
}