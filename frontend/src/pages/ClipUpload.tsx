import {useState} from "react";
import {useNavigate} from "react-router-dom";
import BlueButton from "../components/buttons/BlueButton.tsx";
import Box from "../components/Box.tsx";

const ClipUpload = () => {
    const [file, setFile] = useState<File | null>(null);
    const navigate = useNavigate();
    const [error, setError] = useState<null | string>(null);

    const press = () => {
        if (!file) {
            setError("Please choose a file");
            return;
        }
        navigate('/create/new', { state: { file } });
    };

    return (
        <div className="flex items-start justify-center p-8 min-h-full">
            <Box className="flex flex-col gap-4 p-6 w-full max-w-md">
                <h1 className="text-lg font-semibold text-gray-900">Upload Clip</h1>

                <input
                    type="file"
                    onChange={(e) => {
                        const selected = e.target.files?.[0] ?? null;
                        setFile(selected);
                        setError(null);
                    }}
                    className="h-36 cursor-pointer rounded-lg border-2 border-dashed border-gray-300 bg-gray-50 p-4 text-center text-sm text-gray-500 hover:bg-gray-100 hover:border-primary transition-colors duration-150"
                />

                <BlueButton onClick={press}>Upload</BlueButton>

                {error && <p className="text-sm text-red-500 text-center">{error}</p>}
            </Box>
        </div>
    )
};

export default ClipUpload;
