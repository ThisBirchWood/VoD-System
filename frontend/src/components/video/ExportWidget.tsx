import BlueButton from "../buttons/BlueButton.tsx";
import React from "react";

type props = {
    dataSend: React.MouseEventHandler<HTMLButtonElement>;
    handleDownload: React.MouseEventHandler<HTMLButtonElement>;
    downloadable: boolean;
    progress: number
}

const ExportWidget = ({dataSend, handleDownload, downloadable, progress}: props) => {
    return (
        <div className={"flex flex-col gap-3"}>
            <BlueButton
                onClick={dataSend}>
                Export
            </BlueButton>

            { downloadable ?
                (<BlueButton
                    onClick={handleDownload}>
                    Download
                </BlueButton>)
                :(
                    <progress
                        value={progress}
                        className={"bg-gray-300 rounded-lg h-1"}>
                    </progress> )
            }
        </div>
    )
}

export default ExportWidget;