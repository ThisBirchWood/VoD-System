import clsx from "clsx";
import type {VideoMetadata} from "../../utils/types.ts";

type MetadataBoxProps = {
    setMetadata: Function
    className?: string;
}

const MetadataBox = ({setMetadata, className}: MetadataBoxProps) => {
    return (
        <div className={clsx("flex flex-col content-between p-10 gap-2", className)}>
            {/*<h2 className={"text-2xl font-bold col-span-2"}>Metadata</h2>*/}

            <p className={"w-full font-bold text-xl "}>Title</p>
            <input
                type="text"
                placeholder="Enter title"
                onChange={(e) => setMetadata((prevState: VideoMetadata) => ({
                    ...prevState,
                    title: e.target.value
                }))}
                className={"border-black bg-gray-200 rounded-md w-full p-2"}
            />
        </div>
    )
}

export default MetadataBox;