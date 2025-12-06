import RangeSlider from 'react-range-slider-input';
import 'react-range-slider-input/dist/style.css';
import {useRef} from "react";
import clsx from 'clsx';
import type { VideoMetadata } from "../../utils/types.ts";

type Props = {
    videoRef: HTMLVideoElement | null;
    videoMetadata: VideoMetadata;
    setSliderValue: Function;
    setMetadata: Function;
    className?: string;
};

export default function ClipRangeSlider({videoRef,
                                            videoMetadata,
                                            setSliderValue,
                                            setMetadata,
                                            className}: Props) {
    const previousRangeSliderInput = useRef<[number, number]>([0, 0]);

    const handleRangeSliderInput = (val: [number, number]) => {
        if (!videoRef) return;

        if (previousRangeSliderInput.current[0] != val[0]) {
            videoRef.currentTime = val[0];
            setSliderValue(val[0]);
        } else if (previousRangeSliderInput.current[1] != val[1]) {
            videoRef.currentTime = val[1];
            setSliderValue(val[1]);
        }

        setMetadata((prevState: VideoMetadata) => ({
            ...prevState,
            startPoint: val[0],
            duration: val[1] - val[0]
        }
        ))
        previousRangeSliderInput.current = val;
    };

    return (
        <RangeSlider
            min={0}
            max={videoMetadata.duration}
            step={0.1}
            onInput={handleRangeSliderInput}
            className={clsx(className)}
            id={"range-slider"}
        />
    )
}