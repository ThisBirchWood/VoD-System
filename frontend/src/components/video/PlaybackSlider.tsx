import {useEffect} from "react";
import clsx from 'clsx';
import type { VideoMetadata } from "../../utils/types.ts";

type Props = {
    videoRef: HTMLVideoElement | null;
    videoMetadata: VideoMetadata;
    sliderValue: number;
    setSliderValue: Function;
    className?: string;
};

export default function PlaybackSlider({videoRef,
                                           videoMetadata,
                                           sliderValue,
                                           setSliderValue,
                                           className}: Props) {
    const updateVideo = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!videoRef) return;

        videoRef.currentTime = e.target.valueAsNumber;
        setSliderValue(e.target.valueAsNumber);
    }

    // update slider
    useEffect(() => {
        if (!videoRef) return;

        const updateSlider = () => {
            setSliderValue(videoRef.currentTime);
        };

        videoRef.addEventListener("timeupdate", updateSlider);

        return () => {
            videoRef.removeEventListener("timeupdate", updateSlider);
        };
    }, [videoRef]);

    return (
        <input
            type={"range"}
            min={0}
            max={videoMetadata.endPoint}
            value={sliderValue}
            onChange={updateVideo}
            step={0.1}
            className={clsx(className)}
        />
    )
}