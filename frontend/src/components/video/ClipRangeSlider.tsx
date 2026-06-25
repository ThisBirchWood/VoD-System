import RangeSlider from 'react-range-slider-input';
import 'react-range-slider-input/dist/style.css';
import { useRef, type Dispatch, type SetStateAction } from "react";
import type { VideoMetadata } from "../../utils/types.ts";

type Props = {
    videoRef: HTMLVideoElement | null;
    videoMetadata: VideoMetadata;
    setSliderValue: Dispatch<SetStateAction<number>>;
    setMetadata: Dispatch<SetStateAction<VideoMetadata>>;
    className?: string;
};

export default function ClipRangeSlider({ videoRef, videoMetadata, setSliderValue, setMetadata, className }: Props) {
    const previousRange = useRef<[number, number]>([0, 0]);

    const handleRangeInput = (val: [number, number]) => {
        if (!videoRef) return;

        if (previousRange.current[0] !== val[0]) {
            videoRef.currentTime = val[0];
            setSliderValue(val[0]);
        } else if (previousRange.current[1] !== val[1]) {
            videoRef.currentTime = val[1];
            setSliderValue(val[1]);
        }

        setMetadata(prev => ({ ...prev, startPoint: val[0], duration: val[1] - val[0] }));
        previousRange.current = val;
    };

    return (
        <RangeSlider
            min={0}
            max={videoMetadata.duration}
            step={0.1}
            onInput={handleRangeInput}
            className={className}
            id="range-slider"
        />
    );
}
