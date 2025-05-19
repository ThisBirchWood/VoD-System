import {useState} from "react";
import {UploadService} from "Frontend/generated/endpoints";
import { useNavigate } from 'react-router-dom';
import "./../index.css";

export default function main() {
    const [file, setFile] = useState<File | null>(null);
    const navigate = useNavigate();

    const [error, setError] = useState(false);

    function press() {
        if (file) {
            UploadService.upload(file)
                .then(uuid => navigate(`video/${uuid}`))
                .catch(e => console.error(e));
        } else {
            setError(true);
        }
    }

    return (
        <div className={"flex flex-col justify-between"}>
            <input
                type="file"
                onChange={(e) => {
                    const selected = e.target.files?.[0] ?? null;
                    setFile(selected);
                }}
                className={"block w-full cursor-pointer rounded-lg border border-dashed border-gray-400 bg-white p-4 text-center hover:bg-gray-50 transition"}
            />
            <button
                onClick={() => press()}
                className={"text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800"}
            >Upload</button>

            {error &&
                <label className={"text-center text-red-500"}>Please choose a file</label>
            }

        </div>
    )
}