import BlueButton from "../buttons/BlueButton.tsx";
import React from "react";

type props = {
    dataSend: React.MouseEventHandler<HTMLButtonElement>;
    handleDownload: React.MouseEventHandler<HTMLButtonElement>;
    downloadable: boolean;
    progress: number;
    uploading: boolean;
}

const Spinner = () => (
    <svg
        className="animate-spin h-4 w-4"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
    >
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
    </svg>
);

const ExportWidget = ({dataSend, handleDownload, downloadable, progress, uploading}: props) => {
    return (
        <div className={"flex flex-col gap-3"}>
            <BlueButton
                onClick={dataSend}
                disabled={uploading}
                className={uploading ? "opacity-60 cursor-not-allowed flex items-center justify-center gap-2" : ""}
            >
                {uploading && <Spinner />}
                {uploading ? "Uploading..." : "Export"}
            </BlueButton>

            <div className="h-10 flex items-center">
                { downloadable ?
                    (<BlueButton className="w-full" onClick={handleDownload}>
                        Download
                    </BlueButton>)
                    :(
                        <progress
                            value={progress}
                            className="w-full h-1.5 rounded bg-gray-200">
                        </progress>
                    )
                }
            </div>
        </div>
    )
}

export default ExportWidget;
