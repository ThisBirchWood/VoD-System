import type { VideoMetadata } from "../utils/types.ts";
import clsx from "clsx";

type prop = {
    setMetadata: Function;
    className?: string;
}

export default function ClipConfig({setMetadata, className}: prop) {
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
        <div className={clsx("flex flex-col gap-2 p-10 rounded-md", className)}>
            <h2 className={"text-3xl font-bold text-gray-800 mb-4 underline"}>Clip Export Settings</h2>
            <div className="flex items-center gap-2">
                <label htmlFor="resolution"
                       className={"w-full"}
                >Resolution: </label>
                <select id="resolution"
                        name="resolution"
                        defaultValue="1280,720"
                        onChange={updateRes}
                        className={"border-black bg-gray-200 p-2 rounded-md w-full"}>
                    <option value="3840,2160">2160p (4K)</option>
                    <option value="2560,1440">1440p (QHD)</option>
                    <option value="1920,1080">1080p (Full HD)</option>
                    <option value="1280,720">720p (HD)</option>
                    <option value="854,480">480p (SD)</option>
                    <option value="640,360">360p (Low)</option>
                </select>
            </div>

            <div className="flex items-center gap-2">
                <label htmlFor="fps"
                       className={"w-full"}
                >FPS: </label>
                <select id="fps"
                        name="fps"
                        defaultValue="30"
                        onChange={updateFps}
                        className={"border-black bg-gray-200 p-2 rounded-md w-full"}>
                    <option value="60">60</option>
                    <option value="30">30</option>
                    <option value="15">15</option>
                </select>
            </div>

            <div className="flex items-center gap-2">
                <label className={"w-full"}>
                    File Size (mb):
                </label>
                <input type="number"
                       min="1"
                       defaultValue="10"
                       onChange={updateFileSize}
                       className={"border-black bg-gray-200 p-2 rounded-md w-full"}
                />
            </div>
        </div>
    )
}