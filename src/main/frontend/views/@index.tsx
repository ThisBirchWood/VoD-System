import {useState} from "react";
import {UploadService} from "Frontend/generated/endpoints";

export default function main() {
    const [uuid, setUuid] = useState<String | undefined> (undefined);
    const [file, setFile] = useState<File | null>(null);

    function press() {
        if (file) {
            UploadService.upload(file)
                .then(uuid => setUuid(uuid))
                .catch(e => console.error(e));
        }
    }

    return (
        <div>

            <video width={640} height={480} controls>
                { (uuid) &&
                    <source src={`/download/input/${uuid}`} />
                }
            </video>

            <input
                type="file"
                onChange={(e) => {
                    const selected = e.target.files?.[0] ?? null;
                    setFile(selected);
                }}
            />
            <button onClick={() => press()}>Upload</button>

        </div>
    )
}