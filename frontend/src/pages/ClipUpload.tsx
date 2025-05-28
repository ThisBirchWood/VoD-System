import {useState} from "react";
import {useNavigate} from "react-router-dom";
import { uploadFile } from "../utils/endpoints"
import BlueButton from "../components/buttons/BlueButton.tsx";

const clipUpload = () => {
    const [file, setFile] = useState<File | null>(null);
    const navigate = useNavigate();

    const [noFileError, setNoFileError] = useState(false);
    const press = (() => {
        if (file) {
            uploadFile(file)
                .then(uuid => navigate(`/create/${uuid}`))
                .catch(e => console.error(e));
        } else {
            setNoFileError(true);
        }
    });

    return (
        <div className={"flex flex-col justify-between gap-3"}>
            <input
                type="file"
                onChange={(e) => {
                    const selected = e.target.files?.[0] ?? null;
                    setFile(selected);
                }}
                className={"h-100 cursor-pointer rounded-lg border border-dashed border-gray-400 bg-white p-4 text-center hover:bg-gray-50 transition"}
            />

            <BlueButton
                onClick={press}
            >Upload</BlueButton>

            {noFileError &&
                <label className={"text-center text-red-500"}>Please choose a file</label>
            }

        </div>
    )
};

export default clipUpload;
