import type { VideoMetadata } from "../../utils/types.ts";
import Selector from "../Selector.tsx";
import clsx from "clsx";

type prop = {
    setMetadata: Function;
    className?: string;
}

export default function ConfigBox({setMetadata, className}: prop) {
    const updateRes = (e: React.ChangeEvent<HTMLSelectElement>) => {
        var vals = e.target.value.split(",");
        setMetadata((prevState: VideoMetadata) => ({
            ...prevState,
            width: parseInt(vals[0]),
            height: parseInt(vals[1])
        }))
    }

    const updateFps = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setMetadata((prevState: VideoMetadata) => ({
            ...prevState,
            fps: parseInt(e.target.value)
        }))
    }

    const updateFileSize = (e: React.ChangeEvent<HTMLInputElement>) => {
        setMetadata((prevState: VideoMetadata) => ({
            ...prevState,
            fileSize: parseInt(e.target.value) * 1000
        }))
    }

    return (
        <div className={clsx("flex flex-col gap-2 p-10", className)}>
            <h2 className={"text-3xl font-bold mb-4"}>Export Settings</h2>

            <Selector label={"Resolution"}>
                <select id="resolution"
                        name="resolution"
                        defaultValue="1280,720"
                        onChange={updateRes}
                        className={"border-black bg-gray-200 rounded-md w-full"}>
                    <option value="3840,2160">2160p</option>
                    <option value="2560,1440">1440p</option>
                    <option value="1920,1080">1080p</option>
                    <option value="1280,720">720p</option>
                    <option value="854,480">480p</option>
                    <option value="640,360">360p</option>
                </select>
            </Selector>

            <Selector label={"FPS"}>
                <select id="fps"
                        name="fps"
                        defaultValue="30"
                        onChange={updateFps}
                        className={"border-black bg-gray-200 rounded-md w-full"}>
                    <option value="60">60</option>
                    <option value="30">30</option>
                    <option value="15">15</option>
                </select>
            </Selector>

            <Selector label={"File Size Limit (MB)"}>
                <input type="number"
                       min="1"
                       defaultValue="10"
                       onChange={updateFileSize}
                       className={"border-black bg-gray-200 rounded-md w-full"}
                />
            </Selector>

        </div>
    )
}