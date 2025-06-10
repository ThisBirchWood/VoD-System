import {useState} from "react";
import {useNavigate} from "react-router-dom";
import { uploadFile } from "../utils/endpoints"
import BlueButton from "../components/buttons/BlueButton.tsx";
import Box from "../components/Box.tsx";

const clipUpload = () => {
    const [file, setFile] = useState<File | null>(null);
    const navigate = useNavigate();

    const [error, setError] = useState<null | string>(null);
    const press = (() => {
        if (file) {
            uploadFile(file, setError)
                .then(uuid => navigate(`/create/${uuid}`))
                .catch(e => console.error(e));
        } else {
            setError("Please choose a file");
        }
    });

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
        </Box>
    )
};

export default clipUpload;
