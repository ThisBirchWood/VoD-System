export default function ClipConfig() {
    return (
        <div className={"flex flex-col gap-2"}>
            <div className="flex items-center gap-2">
                <label htmlFor="resolution"
                       className={"w-24"}
                >Resolution: </label>
                <select id="resolution"
                        name="resolution"
                        defaultValue="1280,720"
                        className={"border-black bg-gray-200 p-1 rounded-md"}>
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
                       className={"w-24"}
                >FPS: </label>
                <select id="fps"
                        name="fps"
                        defaultValue="30"
                        className={"border-black bg-gray-200 p-1 rounded-md"}>
                    <option value="60">60</option>
                    <option value="30">30</option>
                    <option value="15">15</option>
                </select>
            </div>

            <div className="flex items-center gap-2">
                <label className={"w-24"}>
                    File Size (mb):
                </label>
                <input type="number"
                       min="1"
                       defaultValue="10"
                       className={"border-black bg-gray-200 p-1 rounded-md"}
                />
            </div>
        </div>
    )
}