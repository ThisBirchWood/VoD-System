import {useState} from "react";
import {useNavigate} from "react-router-dom";
import {uploadFile, convertFile, getProgress} from "../utils/endpoints"
import BlueButton from "../components/buttons/BlueButton.tsx";
import Box from "../components/Box.tsx";

const ClipUpload = () => {
    const [file, setFile] = useState<File | null>(null);
    const navigate = useNavigate();
    const [error, setError] = useState<null | string>(null);
    const [progress, setProgress] = useState<number>(0);

    const isVideoFileSupported = (file: File): boolean => {
        const video = document.createElement("video");

        if (file.type && video.canPlayType(file.type) !== "") {
            return true;
        }

        const extension = file.name.split(".").pop()?.toLowerCase();
        const extensionToMime: Record<string, string> = {
            mp4: "video/mp4",
            webm: "video/webm",
            ogg: "video/ogg",
            mov: "video/quicktime",
        };

        if (extension && extensionToMime[extension]) {
            return video.canPlayType(extensionToMime[extension]) !== "";
        }

        return false;
    };

    const press = (() => {
        if (!file) {
            setError("Please choose a file");
            return;
        }


        uploadFile(file)
            .then(uuid => {

                if (isVideoFileSupported(file)) {
                    navigate(`/create/${uuid}`)
                } else {
                    convertFile(uuid);
                    const interval = setInterval(async() => await pollProgress(uuid, interval), 500);
                }

            })
            .catch((e: Error) => setError(`Failed to upload file: ${e.message}`));

    });

    const pollProgress = async (id: string, intervalId: number) => {
        getProgress(id)
            .then((progress) => {
                setProgress(progress.conversion.progress);

                if (progress.conversion.complete) {
                    clearInterval(intervalId);
                    navigate(`/create/${id}`)
                }
            })
            .catch((err: Error) => {
                setError(`Failed to fetch progress: ${err.message}`);
                clearInterval(intervalId);
            });
    }

    return (
        <Box className={"flex flex-col justify-between gap-3 p-5"}>
            <input
                type="file"
                onChange={(e) => {
                    const selected = e.target.files?.[0] ?? null;
                    setFile(selected);
                }}
                className={"h-100 cursor-pointer rounded-lg border border-dashed border-gray-400 bg-white p-4 text-center hover:bg-gray-50 transition"}
            />

            <BlueButton
                onClick={press}>
                Upload
            </BlueButton>

            <label className={"text-center text-red-500"}>{error}</label>
            <progress
                value={progress}
                className={"bg-gray-300 rounded-lg h-1"}>
            </progress>
        </Box>
    )
};

export default ClipUpload;
