const Home = () => {
    return (
        <div className="max-h-screen flex flex-col justify-center items-center px-6 py-12 text-gray-900">
            {/* Logo */}
            <img
                src="/logo.png"
                alt="VoD System Logo"
                className="h-36 mb-8 object-contain"
            />

            {/* Description Container */}
            <div className="max-w-xl text-center">
                <h2 className="text-3xl font-semibold mb-6 text-gray-700">
                    What is the VoD System?
                </h2>
                <p className="text-lg leading-relaxed text-gray-600 mb-6">
                    The VoD System is a powerful clip management platform designed to streamline how you
                    handle your video content. Whether you're a content creator, streamer, or educator,
                    VoD System lets you:
                </p>

                <ul className="list-disc list-inside text-gray-600 mb-8 space-y-2">
                    <li>Upload clips effortlessly and securely.</li>
                    <li>Edit and trim videos with intuitive controls.</li>
                    <li>Compress files to specific file sizes.</li>
                    <li>Organize your clips for quick access and sharing.</li>
                </ul>

                <p className="text-lg leading-relaxed text-gray-600">
                    Designed with simplicity and efficiency in mind, VoD System adapts to your workflow,
                    making video clip management faster and more enjoyable than ever.
                </p>
            </div>

            {/* File Support Note */}
            <div className="bg-gray-100 border border-gray-300 rounded-md p-4 text-sm text-gray-500 max-w-md mx-auto">
                <strong>Note:</strong> Currently, only <code>.mp4</code> files are supported for upload and processing.
                Support for additional video formats will be added in future updates.
            </div>
        </div>
    );
};

export default Home;
