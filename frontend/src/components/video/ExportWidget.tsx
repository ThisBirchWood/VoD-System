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