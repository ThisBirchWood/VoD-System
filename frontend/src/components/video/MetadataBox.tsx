import clsx from "clsx";
import type {VideoMetadata} from "../../utils/types.ts";
import Selector from "../Selector.tsx";

type MetadataBoxProps = {
    setMetadata: Function
    className?: string;
}

const MetadataBox = ({setMetadata, className}: MetadataBoxProps) => {
    return (
        <div className={clsx("flex flex-col content-between p-10 gap-2", className)}>
            <h2 className={"text-3xl font-bold mb-4 col-span-2"}>Metadata Settings</h2>

            <Selector label={"Title"}>
                <input
                    type="text"
                    placeholder="Enter title"
                    onChange={(e) => setMetadata((prevState: VideoMetadata) => ({
                        ...prevState,
                        title: e.target.value
                    }))}
                    className={"border-black bg-gray-200 rounded-md w-full p-2"}
                />
            </Selector>

            <Selector label={"Description"}>
                <textarea
                    placeholder="Enter description"
                    onChange={(e) => setMetadata((prevState: VideoMetadata) => ({
                        ...prevState,
                        description: e.target.value
                    }))}
                    className={"border-black bg-gray-200 rounded-md w-full p-2 pb-10 resize-none max-h"}
                />
            </Selector>
        </div>
    )
}

export default MetadataBox;