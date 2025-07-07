import clsx from "clsx";
import type {VideoMetadata} from "../../utils/types.ts";

type ClipNamesProps = {
    setMetadata: Function
    className?: string;
}

const ClipNames = ({setMetadata, className}: ClipNamesProps) => {
    return (
        <div className={clsx("flex flex-col gap-2 p-10", className)}>
            <label>Title: </label>
            <input
                type="text"
                placeholder="Enter title"
                onChange={(e) => setMetadata((prevState: VideoMetadata) => ({
                    ...prevState,
                    title: e.target.value
                }))}
                className={"border-black bg-gray-200 rounded-md w-full p-2"}
            />

            <label>Description: </label>
            <input
                type="text"
                placeholder="Enter description"
                onChange={(e) => setMetadata((prevState: VideoMetadata) => ({
                    ...prevState,
                    description: e.target.value
                }))}
                className={"border-black bg-gray-200 rounded-md w-full p-2"}
            />
        </div>
    )
}

export default ClipNames;